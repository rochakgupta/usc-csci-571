import express, { Request } from 'express';
import { Response, Parser } from './common';
import * as Service from './service';

const api = express.Router();

api.get('/search', async (req: Request, res) => {
    try {
        const query = Parser.parseStringParameter(req.query.query);
        const data = await Service.search(query);
        return Response.sendOk(res, data);
    } catch (error) {
        return Response.sendError(res, error);
    }
});

api.get('/details-summary/:ticker', async (req: Request, res) => {
    try {
        const ticker = Parser.parseStringParameter(req.params.ticker);
        const data = await Service.getDetailsAndSummary(ticker);
        return Response.sendOk(res, data);
    } catch (error) {
        return Response.sendError(res, error);
    }
});

api.get('/news/:ticker', async (req: Request, res) => {
    try {
        const ticker = Parser.parseStringParameter(req.params.ticker);
        const data = await Service.getNews(ticker);
        return Response.sendOk(res, data);
    } catch (error) {
        return Response.sendError(res, error);
    }
});

api.get('/historical-chart/:ticker', async (req: Request, res) => {
    try {
        const ticker = Parser.parseStringParameter(req.params.ticker);
        const data = await Service.getHistoricalChartData(ticker);
        return Response.sendOk(res, data);
    } catch (error) {
        return Response.sendError(res, error);
    }
});

api.get('/last-price', async (req: Request, res) => {
    try {
        const tickers = Parser.parseArrayParameter(req.query.tickers);
        const data = await Service.getLastPrices(tickers);
        return Response.sendOk(res, data);
    } catch (error) {
        return Response.sendError(res, error);
    }
});

export default api;
