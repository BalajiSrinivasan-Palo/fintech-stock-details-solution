import { render, screen } from "@testing-library/react";
import FundTable from "../FundTable";
import { FundBreakdown } from "../../types/portfolio";
import { LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";

describe("FundTable", () => {
  const mockData: FundBreakdown[] = [
    {
      date: "2024-01-01",
      fundName: "Test Fund",
      totalValue: 1000.0,
      breakdown: [
        {
          symbol: "AAPL",
          proportion: 50.0,
          price: 150.0,
          value: 750.0,
        },
      ],
    },
  ];

  const defaultProps = {
    data: mockData,
    loading: false,
    error: null,
    totalElements: 1,
    page: 0,
    rowsPerPage: 10,
    fundName: "",
    onPageChange: jest.fn(),
    onRowsPerPageChange: jest.fn(),
    onStartDateChange: jest.fn(),
    onEndDateChange: jest.fn(),
    onFundNameChange: jest.fn(),
    startDate: null,
    endDate: null,
  };

  const renderWithProvider = (ui: React.ReactElement) => {
    return render(
      <LocalizationProvider dateAdapter={AdapterDateFns}>
        {ui}
      </LocalizationProvider>
    );
  };

  it("renders table with fund data correctly", () => {
    renderWithProvider(<FundTable {...defaultProps} />);
    expect(screen.getByText("Fund Portfolio Data")).toBeInTheDocument();
  });
});
