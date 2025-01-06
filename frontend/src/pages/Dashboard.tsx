import { ThemeProvider, createTheme } from "@mui/material";
import { QueryClient, QueryClientProvider } from "react-query";
import { LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import Layout from "../components/Layout";
import FundValuesTable from "../components/FundValuesTable";

const theme = createTheme({
  palette: {
    primary: { main: "#1976d2" },
  },
});

const queryClient = new QueryClient();

export const Dashboard = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <LocalizationProvider dateAdapter={AdapterDateFns}>
        <ThemeProvider theme={theme}>
          <Layout>
            <FundValuesTable />
          </Layout>
        </ThemeProvider>
      </LocalizationProvider>
    </QueryClientProvider>
  );
};
