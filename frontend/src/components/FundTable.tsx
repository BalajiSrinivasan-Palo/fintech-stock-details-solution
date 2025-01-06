import React from "react";
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Box,
  Typography,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  TextField,
  Stack,
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers";
import { FundData } from "../services/api";
import { format } from "date-fns";

interface FundTableProps {
  data: FundData[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  page: number;
  rowsPerPage: number;
  startDate: Date | null;
  endDate: Date | null;
  fundName: string;
  onPageChange: (page: number) => void;
  onRowsPerPageChange: (rowsPerPage: number) => void;
  onStartDateChange: (date: Date | null) => void;
  onEndDateChange: (date: Date | null) => void;
  onFundNameChange: (name: string) => void;
}

const BreakdownCard = ({
  symbol,
  proportion,
  price,
  value,
}: {
  symbol: string;
  proportion: number;
  price: number;
  value: number;
}) => (
  <Card sx={{ mb: 1, backgroundColor: "#f5f5f5" }}>
    <CardContent>
      <Typography variant="subtitle2">[{symbol}]</Typography>
      <Typography variant="body2">
        Proportion: {proportion.toFixed(2)}%
      </Typography>
      <Typography variant="body2">
        Price: $
        {price.toLocaleString(undefined, {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        })}
      </Typography>
      <Typography variant="body2">
        Value: $
        {value.toLocaleString(undefined, {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        })}
      </Typography>
    </CardContent>
  </Card>
);

const FundTable: React.FC<FundTableProps> = ({
  data,
  loading,
  error,
  totalElements,
  page,
  rowsPerPage,
  startDate,
  endDate,
  fundName,
  onPageChange,
  onRowsPerPageChange,
  onStartDateChange,
  onEndDateChange,
  onFundNameChange,
}) => {
  if (loading) {
    return (
      <Box display="flex" justifyContent="center" p={3}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  return (
    <Box sx={{ width: "100%", p: 2 }}>
      <Typography variant="h5" gutterBottom>
        Fund Portfolio Data
      </Typography>

      <Stack direction="row" spacing={2} sx={{ mb: 3 }}>
        <TextField
          label="Fund Name"
          value={fundName}
          onChange={(e) => onFundNameChange(e.target.value)}
          size="small"
        />
        <DatePicker
          label="Start Date"
          value={startDate}
          onChange={onStartDateChange}
          slotProps={{ textField: { size: "small" } }}
        />
        <DatePicker
          label="End Date"
          value={endDate}
          onChange={onEndDateChange}
          slotProps={{ textField: { size: "small" } }}
        />
      </Stack>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Date</TableCell>
              <TableCell>Fund Name</TableCell>
              <TableCell align="right">Total Value</TableCell>
              <TableCell>Breakdown</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data.map((fund, index) => (
              <TableRow key={index}>
                <TableCell>
                  {" "}
                  {format(new Date(fund.date), "yyyy-MM-dd")}
                </TableCell>
                <TableCell>{fund.fundName}</TableCell>
                <TableCell align="right">
                  ${fund.totalValue.toFixed(2)}
                </TableCell>
                <TableCell>
                  <Box sx={{ maxWidth: 300 }}>
                    {fund.breakdown.map((holding) => (
                      <BreakdownCard
                        key={holding.symbol}
                        symbol={holding.symbol}
                        proportion={holding.proportion}
                        price={holding.price}
                        value={holding.value}
                      />
                    ))}
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <TablePagination
        component="div"
        count={totalElements}
        page={page}
        onPageChange={(_, newPage) => onPageChange(newPage)}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={(e) =>
          onRowsPerPageChange(parseInt(e.target.value, 10))
        }
        rowsPerPageOptions={[5, 10, 25]}
      />
    </Box>
  );
};

export default FundTable;
