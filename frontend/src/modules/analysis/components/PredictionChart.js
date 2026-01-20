import React from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import { format } from 'date-fns';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const PredictionChart = ({ measurements, prediction }) => {
  if (!measurements || measurements.length === 0) {
    return <div>Brak danych do wyświetlenia</div>;
  }

  const sortedMeasurements = [...measurements].sort((a, b) => 
    new Date(a.timestamp) - new Date(b.timestamp)
  );

  // Determine prediction value safely
  const predValue = prediction?.value ?? prediction?.prediction?.value ?? null;

  // Compute prediction timestamp from measurements (last + interval)
  const len = sortedMeasurements.length;
  const last = new Date(sortedMeasurements[len - 1].timestamp);
  let predictionDate = null;
  if (len >= 2) {
    const prev = new Date(sortedMeasurements[len - 2].timestamp);
    const interval = last.getTime() - prev.getTime();
    const safeInterval = interval > 0 ? interval : 60 * 60 * 1000;
    predictionDate = new Date(last.getTime() + safeInterval);
  } else {
    predictionDate = new Date(last.getTime() + 60 * 60 * 1000);
  }

  const labels = [
    ...sortedMeasurements.map(m => format(new Date(m.timestamp), 'dd.MM HH:mm')),
    format(predictionDate, 'dd.MM HH:mm'),
  ];

  const historicalData = sortedMeasurements.map(m => m.gridConsumption);

  // Make prediction dataset null for historical points and set only the predicted value at the final label
  const predictionData = new Array(historicalData.length).fill(null).concat(predValue != null ? predValue : null);

  // Debugging info (visible in browser console) to help verify alignment
  // eslint-disable-next-line no-console
  console.debug('PredictionChart:', {
    lastMeasurements: sortedMeasurements.slice(-3).map(m => m.timestamp),
    predictionDate: predictionDate && predictionDate.toISOString(),
    labelsCount: labels.length,
    historicalLength: historicalData.length,
    predictionDataLength: predictionData.length,
  });

  const data = {
    labels,
    datasets: [
      {
        label: 'Historyczne zużycie (kWh)',
        data: historicalData,
        borderColor: 'rgb(102, 126, 234)',
        backgroundColor: 'rgba(102, 126, 234, 0.1)',
        tension: 0.4,
        fill: true,
      },
      {
        label: 'Prognoza (kWh)',
        data: predictionData,
        borderColor: 'rgb(255, 99, 132)',
        backgroundColor: 'rgba(255, 99, 132, 0.1)',
        borderDash: [5, 5],
        tension: 0.4,
        pointRadius: 6,
        pointHoverRadius: 8,
        spanGaps: false,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
      },
      title: {
        display: false,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        title: {
          display: true,
          text: 'Zużycie (kWh)',
        },
      },
      x: {
        title: {
          display: true,
          text: 'Czas',
        },
      },
    },
  };

  return (
    <div style={{ height: '400px' }}>
      <Line data={data} options={options} />
    </div>
  );
};

export default PredictionChart;

