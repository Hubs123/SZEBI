import React from "react";
import { Link } from "react-router-dom";

const DeviceGroupList = ({ groups }) => {
    return (
        <ul>
            {groups.map(group => (
                <li key={group.id}>
                    <Link to={`/alerts/groups/${group.id}`}>
                        {group.groupName}
                    </Link>
                </li>
            ))}
        </ul>
    );
};

export default DeviceGroupList;
