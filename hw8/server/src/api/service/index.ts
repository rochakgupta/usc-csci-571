import moment from 'moment-timezone';
import { Parser, Tiingo, NewsAPI } from '../common';
import { SearchResultItem, Details, Summary, ChartItem, DetailsAndSummary, NewsItem, LastPrices } from './models';

export const search = async (query: string): Promise<SearchResultItem[]> => {
    const items = await Tiingo.search(query);
    const searchResultItems: SearchResultItem[] = [];
    items.forEach(item => {
        try {
            searchResultItems.push({
                ticker: Parser.parseString(item.ticker),
                name: Parser.parseString(item.name)
            });
        } catch (error) {
            // Skip item
        }
    });
    return Parser.parseArray(searchResultItems);
};

const buildDetails = (metadata: any, currentTopOfBookAndLastPrice: any): Details => {
    const ticker = Parser.parseString(metadata.ticker);
    const name = Parser.parseString(metadata.name);
    const exchangeCode = Parser.parseString(metadata.exchangeCode);

    const lastPrice = Parser.parseNumber(currentTopOfBookAndLastPrice.last);
    const prevClose = Parser.parseNumber(currentTopOfBookAndLastPrice.prevClose);
    const timestamp = Parser.parseString(currentTopOfBookAndLastPrice.timestamp);

    const change = Parser.parseNumber(lastPrice - prevClose);
    const changePercent = Parser.parseNumber((change * 100) / prevClose);

    const lastTimestampMoment = moment(timestamp).tz('America/Los_Angeles');
    const currentTimestampMoment = moment().tz('America/Los_Angeles');
    const isMarketOpen = currentTimestampMoment.diff(lastTimestampMoment, 'seconds') < 60;

    const lastTimestamp = lastTimestampMoment.format('YYYY-MM-DD HH:mm:ss');
    const currentTimestamp = currentTimestampMoment.format('YYYY-MM-DD HH:mm:ss');

    return {
        ticker,
        name,
        exchangeCode,
        lastPrice,
        change,
        changePercent,
        currentTimestamp,
        isMarketOpen,
        lastTimestamp
    };
};

const getSummaryChartData = async (ticker: string, startDate: string): Promise<ChartItem[]> => {
    const items = await Tiingo.getLastDayPrices(ticker, startDate);
    const parsedItems = items.map((item: any) => ({
        date: moment(Parser.parseString(item.date)).tz('America/Los_Angeles').valueOf(),
        close: Parser.parseNumber(item.close)
    }));
    return parsedItems;
};

const buildSummary = async (metadata: any, currentTopOfBookAndLastPrice: any): Promise<Summary> => {
    const ticker = Parser.parseString(metadata.ticker);

    const timestamp = Parser.parseString(currentTopOfBookAndLastPrice.timestamp);
    const timestampMoment = moment(timestamp).tz('America/Los_Angeles');
    const currentTimestampMoment = moment().tz('America/Los_Angeles');
    const isMarketOpen = currentTimestampMoment.diff(timestampMoment, 'seconds') < 60;

    let summary: Summary = {
        startDate: Parser.parseString(metadata.startDate),
        description: Parser.parseString(metadata.description),
        highPrice: Parser.parseNumber(currentTopOfBookAndLastPrice.high),
        lowPrice: Parser.parseNumber(currentTopOfBookAndLastPrice.low),
        openPrice: Parser.parseNumber(currentTopOfBookAndLastPrice.open),
        prevClose: Parser.parseNumber(currentTopOfBookAndLastPrice.prevClose),
        volume: Parser.parseNumber(currentTopOfBookAndLastPrice.volume),
        chartData: await getSummaryChartData(ticker, timestampMoment.format('YYYY-MM-DD'))
    };

    if (isMarketOpen) {
        let midPrice: number;
        try {
            midPrice = Parser.parseNumber(currentTopOfBookAndLastPrice.mid);
        } catch (error) {
            midPrice = null;
        }
        summary = {
            ...summary,
            midPrice,
            askPrice: Parser.parseNumber(currentTopOfBookAndLastPrice.askPrice),
            askSize: Parser.parseNumber(currentTopOfBookAndLastPrice.askSize),
            bidPrice: Parser.parseNumber(currentTopOfBookAndLastPrice.bidPrice),
            bidSize: Parser.parseNumber(currentTopOfBookAndLastPrice.bidSize)
        };
    }

    return summary;
};

export const getDetailsAndSummary = async (ticker: string): Promise<DetailsAndSummary> => {
    const [
        metadata,
        currentTopOfBookAndLastPrice
    ] = await Promise.all([
        Tiingo.getMetadata(ticker),
        Tiingo.getCurrentTopOfBookAndLastPrice(ticker)
    ]);

    const details = buildDetails(metadata, currentTopOfBookAndLastPrice);
    const summary = await buildSummary(metadata, currentTopOfBookAndLastPrice);

    return {
        details,
        summary
    };
};

export const getNews = async (ticker: string): Promise<NewsItem[]> => {
    const newsItems: NewsItem[] = [];
    let page = 1;
    while (page <= 5 && newsItems.length < 20) {
        const items = await NewsAPI.getEverything(ticker, page);
        for (const item of items) {
            try {
                newsItems.push({
                    publisher: Parser.parseString(item.source.name),
                    publishedDate: moment(Parser.parseString(item.publishedAt)).tz('America/Los_Angeles').format('LL'),
                    title: Parser.parseString(item.title),
                    description: Parser.parseString(item.description),
                    url: Parser.parseString(item.url),
                    urlToImage: Parser.parseString(item.urlToImage)
                });
            } catch (error) {
                // Skip item
            }
            if (newsItems.length === 20) {
                break;
            }
        }
        page += 1;
    }
    return Parser.parseArray(newsItems);
};

export const getHistoricalChartData = async (ticker: string): Promise<ChartItem[]> => {
    const items = await Tiingo.getLastTwoYearPrices(ticker);
    const parsedItems = items.map((item: any) => ({
        date: moment(Parser.parseString(item.date)).tz('America/Los_Angeles').valueOf(),
        open: Parser.parseNumber(item.open),
        high: Parser.parseNumber(item.high),
        low: Parser.parseNumber(item.low),
        close: Parser.parseNumber(item.close),
        volume: Parser.parseNumber(item.volume)
    }));
    return Parser.parseArray(parsedItems);
};

export const getLastPrices = async (tickers: string[]): Promise<LastPrices> => {
    const currentTopOfBookAndLastPrices = await Tiingo.getCurrentTopOfBookAndLastPrices(tickers);
    const lastPrices: LastPrices = {};
    currentTopOfBookAndLastPrices.forEach(currentTopOfBookAndLastPrice => {
        const ticker = Parser.parseString(currentTopOfBookAndLastPrice.ticker);
        const lastPrice = Parser.parseNumber(currentTopOfBookAndLastPrice.last);
        lastPrices[ticker] = lastPrice;
    });
    return lastPrices;
};
