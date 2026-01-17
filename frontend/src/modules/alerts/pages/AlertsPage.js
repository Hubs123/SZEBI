import React, { useEffect, useState } from "react";
import AlertList from "../components/AlertList";
import { getAlerts } from "../../../services/alertsApi";

const AlertsPage = ({ role }) => {
    const [alerts, setAlerts] = useState([]);

    useEffect(() => {
        getAlerts(role).then(setAlerts);
    }, [role]);

    return (
        <div>
            <h2>Notifications</h2>
            <AlertList alerts={alerts} />
        </div>
    );
};

export default AlertsPage;
