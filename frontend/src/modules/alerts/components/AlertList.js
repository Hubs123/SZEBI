import React from "react";
import AlertItem from "./AlertItem";

const AlertList = ({ alerts }) => {
    return (
        <ul>
            {alerts.map(alert => (
                <AlertItem key={alert.id} alert={alert} />
            ))}
        </ul>
    );
};

export default AlertList;
