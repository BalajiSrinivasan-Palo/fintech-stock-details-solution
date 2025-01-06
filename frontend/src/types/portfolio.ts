export interface StockHolding {
  ticker: string;
  value: number;
  quantity: number;
  lastUpdated: string;
}

export interface Portfolio {
  id: string;
  date: string;
  fundName: string;
  totalValue: number;
  breakdown: Record<string, number>;
  holdings: StockHolding[];
  lastUpdated: string;
}

export interface PortfolioError {
  message: string;
  code: string;
  details?: string;
}

export interface FundBreakdown {
  date: string;
  fundName: string;
  totalValue: number;
  breakdown: {
    symbol: string;
    proportion: number;
    price: number;
    value: number;
  }[];
}
