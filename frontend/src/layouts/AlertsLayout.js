import React from "react";
import { Outlet } from "react-router-dom";

const AlertLayout = () => {
    return (
        <div>
            <h1>Moduł alertów</h1>
            <Outlet />
        </div>
    );
};

export default AlertLayout;
