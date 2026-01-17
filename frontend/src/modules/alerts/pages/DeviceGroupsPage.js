import React, { useEffect, useState } from "react";
import DeviceGroupList from "../components/DeviceGroupList";
import { getDeviceGroups } from "../../../services/alertsApi";

const DeviceGroupsPage = () => {
    const [groups, setGroups] = useState([]);

    useEffect(() => {
        getDeviceGroups().then(setGroups);
    }, []);

    return (
        <div>
            <h2>Device Groups</h2>
            <DeviceGroupList groups={groups} />
        </div>
    );
};

export default DeviceGroupsPage;
