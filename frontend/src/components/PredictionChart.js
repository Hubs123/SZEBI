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

  const sortedMeasurements = [...measurements].sort((a, b) => {
    try {
      return new Date(a.timestamp) - new Date(b.timestamp);
    } catch {
      return 0;
    }
  });

  const safeLabel = (ts, index) => {
    try {
      const d = new Date(ts);
      if (isNaN(d.getTime())) return `Okres ${index + 1}`;
      return format(d, 'dd.MM HH:mm');
    } catch {
      return `Okres ${index + 1}`;
    }
  };

  // Dodaj punkt prognozy
  const predictionDate = new Date(prediction.predictedForDate);

  const labels = [
    ...sortedMeasurements.map((m, idx) => safeLabel(m.timestamp, idx)),
    safeLabel(predictionDate, sortedMeasurements.length),
  ];

  // Historyczna seria ma wartości tylko dla punktów historycznych
  const historicalData = sortedMeasurements.map(m => m.gridConsumption);

  // Seria prognozy: null dla historycznych punktów, wartość tylko na końcu
  const predictionData = new Array(sortedMeasurements.length).fill(null).concat([prediction.value]);

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
        spanGaps: true,
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
