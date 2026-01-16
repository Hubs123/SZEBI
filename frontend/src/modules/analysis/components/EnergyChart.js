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

  const sortedMeasurements = [...measurements].sort((a, b) => {
    try {
      return new Date(a.timestamp) - new Date(b.timestamp);
    } catch {
      return 0;
    }
  });

  const data = {
    labels: sortedMeasurements.map((m, index) => {
      try {
        if (!m.timestamp) return `Okres ${index + 1}`;
        const date = new Date(m.timestamp);
        if (isNaN(date.getTime())) return `Okres ${index + 1}`;
        return format(date, 'dd.MM HH:mm');
      } catch (error) {
        console.warn('Błąd formatowania daty:', m.timestamp, error);
        return m.periodNumber ? `Okres ${m.periodNumber}` : `Okres ${index + 1}`;
      }
    }),
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

