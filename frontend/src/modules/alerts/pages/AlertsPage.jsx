import React from 'react';
import AlertList from '../components/AlertList';

const AlertsPage = () => {
    return (
        <div className="panel">
            <h2>Zarządzanie Alertami</h2>
            <p className="mb-4">Poniżej znajduje się lista aktywnych alertów systemowych.</p>

            <AlertList />
        </div>
    );
};

export default AlertsPage;