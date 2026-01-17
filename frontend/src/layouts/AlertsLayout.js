import React from "react";
import { Outlet } from "react-router-dom";

const AlertLayout = () => {
    return (
        <div className="layout-container">
            <h1>Alerts</h1>
            <Outlet />
        </div>
    );
};

export default AlertLayout;
