package com.otus.otuskotlin.stocktrack

import com.otus.otuskotlin.stocktrack.stock.Stock
import com.otus.otuskotlin.stocktrack.stock.StockLock
import com.otus.otuskotlin.stocktrack.stock.BaseStockRepository
import com.otus.otuskotlin.stocktrack.stock.OkStockRepositoryResponse
import com.otus.otuskotlin.stocktrack.stock.OkStocksRepositoryResponse
import com.otus.otuskotlin.stocktrack.stock.StockFilterRepositoryRequest
import com.otus.otuskotlin.stocktrack.stock.StockIdRepositoryRequest
import com.otus.otuskotlin.stocktrack.stock.StockRepositoryRequest
import com.otus.otuskotlin.stocktrack.stock.StockRepositoryResponse
import com.otus.otuskotlin.stocktrack.stock.StocksRepositoryResponse
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class CacheStockRepository(
    ttl: Duration = 2.minutes,
    val randomUuid: () -> String = { UUID.randomUUID().toString() },
) : BaseStockRepository() {
    private val mutex: Mutex = Mutex()
    private val cache = Cache.Builder<String, StockEntity>()
        .expireAfterWrite(ttl)
        .build()

    override suspend fun create(request: StockRepositoryRequest): StockRepositoryResponse {
        return tryReturningOne {
            request.stock.copy(id = Stock.Id(value = randomUuid()), lock = StockLock(value = randomUuid()))
                .also {
                    mutex.withLock(WRITE_ACCESS) {
                        cache.put(it.id.value, StockEntityMapper.toEntity(it))
                    }
                }
                .let { OkStockRepositoryResponse(data = it) }
        }
    }

    override suspend fun findById(request: StockIdRepositoryRequest): StockRepositoryResponse {
        return tryReturningOne {
            request
                .let {
                    mutex.withLock(READ_ACCESS) {
                        cache.get(it.stockId.value)
                    }
                }
                ?.let { OkStockRepositoryResponse(data = StockEntityMapper.fromEntity(it)) }
                ?: stockNotFoundErrorResponse(request.stockId)
        }
    }

    override suspend fun update(request: StockRepositoryRequest): StockRepositoryResponse {
        return tryReturningOne {
            mutex.withLock(WRITE_ACCESS) {
                cache.get(request.stock.id.value)
                    ?.let { stockEntity -> tryUpdateWithLock(request, stockEntity) }
                    ?: stockNotFoundErrorResponse(request.stock.id)
            }
        }
    }

    override suspend fun delete(request: StockIdRepositoryRequest): StockRepositoryResponse {
        return tryReturningOne {
            mutex.withLock(WRITE_ACCESS) {
                cache.get(request.stockId.value)
                    ?.let { tryDeleteWithLock(request, it) }
                    ?: stockNotFoundErrorResponse(request.stockId)
            }
        }
    }

    override suspend fun search(request: StockFilterRepositoryRequest): StocksRepositoryResponse {
        return tryReturningMultiple {
            request
                .let { req ->
                    cache.asMap().values
                        .filter { stock ->
                            req.category
                                .takeIf { category -> category != Stock.Category.NONE }
                                ?.let { category -> category.name == stock.category }
                                ?: true
                        }
                        .filter { stock -> req.name?.let { stock.name.contains(it) } ?: true }
                }
                .map { StockEntityMapper.fromEntity(it) }
                .let { OkStocksRepositoryResponse(data = it) }
        }
    }

    override fun enrich(stocks: Collection<Stock>): Collection<Stock> {
        return stocks.map { stock -> stock.also { cache.put(it.id.value, StockEntityMapper.toEntity(it)) } }
    }

    private fun tryUpdateWithLock(request: StockRepositoryRequest, stockEntity: StockEntity): StockRepositoryResponse {
        return when {
            request.stock.lock.isNone() ->
                emptyRequestLockErrorResponse(request.stock.id)
            stockEntity.lock.isNullOrBlank() ->
                emptyStockLockErrorResponse(request.stock.id)
            request.stock.lock.value != stockEntity.lock ->
                concurrencyErrorResponse(request.stock.id)
            else -> updateStock(stockEntity, request.stock)
        }
    }

    private fun tryDeleteWithLock(request: StockIdRepositoryRequest, stockEntity: StockEntity): StockRepositoryResponse {
        return when {
            request.lock.isNone() ->
                emptyRequestLockErrorResponse(request.stockId)
            stockEntity.lock.isNullOrBlank() ->
                emptyStockLockErrorResponse(request.stockId)
            request.lock.value != stockEntity.lock ->
                concurrencyErrorResponse(request.stockId)
            else -> stockEntity
                .also { cache.invalidate(it.id) }
                .let { OkStockRepositoryResponse(data = StockEntityMapper.fromEntity(it)) }
        }
    }

    private fun updateStock(entity: StockEntity, updateBody: Stock): StockRepositoryResponse {
        return StockEntityMapper.fromEntity(entity)
            .copy(
                lock = StockLock(value = randomUuid()),
                name = updateBody.name,
                category = updateBody.category
            )
            .also { cache.put(updateBody.id.value, StockEntityMapper.toEntity(it)) }
            .let { OkStockRepositoryResponse(data = it) }
    }

    companion object {
        const val READ_ACCESS = "READ_ACCESS"
        const val WRITE_ACCESS = "WRITE_ACCESS"
    }
}