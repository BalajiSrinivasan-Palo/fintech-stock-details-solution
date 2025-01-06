import { useState } from "react";
import { useQuery } from "react-query";
import { getFundBreakdown } from "../services/api";
import FundTable from "./FundTable";

export default function FundValuesTable() {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [startDate, setStartDate] = useState<Date | null>(
    new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
  );
  const [endDate, setEndDate] = useState<Date | null>(new Date());
  const [fundName, setFundName] = useState("");

  const { data, isLoading, error } = useQuery(
    ["fundBreakdown", page, rowsPerPage, startDate, endDate, fundName],
    () =>
      getFundBreakdown({
        page,
        size: rowsPerPage,
        startDate: startDate?.toISOString().split("T")[0],
        endDate: endDate?.toISOString().split("T")[0],
        fundName,
      })
  );

  return (
    <FundTable
      data={data?.content || []}
      loading={isLoading}
      error={error ? "Failed to load data" : null}
      totalElements={data?.totalElements || 0}
      page={page}
      rowsPerPage={rowsPerPage}
      startDate={startDate}
      endDate={endDate}
      fundName={fundName}
      onPageChange={setPage}
      onRowsPerPageChange={setRowsPerPage}
      onStartDateChange={setStartDate}
      onEndDateChange={setEndDate}
      onFundNameChange={setFundName}
    />
  );
}
