import React from "react";

const AlertItem = ({ alert }) => {
    return (
        <li>
            <strong>{alert.priority}</strong> | {alert.anomalyType} | device {alert.deviceId}
        </li>
    );
};

export default AlertItem;
