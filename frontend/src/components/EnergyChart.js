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

const EnergyChart = ({ measurements }) => {
  if (!measurements || measurements.length === 0) {
    return <div>Brak danych do wyświetlenia</div>;
  }

  const sortedMeasurements = [...measurements].sort((a, b) => 
    new Date(a.timestamp) - new Date(b.timestamp)
  );

  const data = {
    labels: sortedMeasurements.map(m => 
      format(new Date(m.timestamp), 'dd.MM HH:mm')
    ),
    datasets: [
      {
        label: 'Zużycie energii (kWh)',
        data: sortedMeasurements.map(m => m.gridConsumption),
        borderColor: 'rgb(102, 126, 234)',
        backgroundColor: 'rgba(102, 126, 234, 0.1)',
        tension: 0.4,
        fill: true,
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

export default EnergyChart;

