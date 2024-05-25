package com.otus.otuskotlin.stocktrack

import com.otus.otuskotlin.stocktrack.context.Context
import com.otus.otuskotlin.stocktrack.context.SearchStocksResponseContext
import com.otus.otuskotlin.stocktrack.cor.chainBuilder
import com.otus.otuskotlin.stocktrack.dsl.command
import com.otus.otuskotlin.stocktrack.dsl.commandPipeline
import com.otus.otuskotlin.stocktrack.dsl.startProcessing
import com.otus.otuskotlin.stocktrack.dsl.stubForDbErrorOnCommand
import com.otus.otuskotlin.stocktrack.dsl.stubForRequestedStubNotFound
import com.otus.otuskotlin.stocktrack.dsl.stubForSucceededSearchCommand
import com.otus.otuskotlin.stocktrack.dsl.stubs
import com.otus.otuskotlin.stocktrack.model.Command
import com.otus.otuskotlin.stocktrack.model.Stock
import com.otus.otuskotlin.stocktrack.model.StockFilter

class SearchStocksResponseProcessor(
    val coreSettings: CoreSettings
) : ResponseProcessor<StockFilter, List<Stock>, SearchStocksResponseContext> {

    override suspend fun execute(context: SearchStocksResponseContext): SearchStocksResponseContext {
        return chainBuilder<SearchStocksResponseContext> {
            startProcessing()

            commandPipeline(Command.SEARCH) {
                stubs {
                    stubForSucceededSearchCommand(coreSettings)
                    stubForDbErrorOnCommand()
                    stubForRequestedStubNotFound()
                }

                command(Command.SEARCH) {
                    copy(
                        response = StubStockRepository.findAll()
                            .filter { stock ->
                                context.request.searchString
                                    ?.let { stock.name.contains(it, true) }
                                    ?: true
                            }
                    )
                }
            }

        }.execute(context)
    }
}
