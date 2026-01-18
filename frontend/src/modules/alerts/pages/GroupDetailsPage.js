import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getThresholds, getReactions } from "../../../services/alertsApi";
import ThresholdList from "../components/ThresholdList";
import ReactionList from "../components/ReactionList";

const GroupDetailsPage = ({ role }) => {
    const { groupId } = useParams();
    const [thresholds, setThresholds] = useState([]);
    const [reactions, setReactions] = useState([]);

    useEffect(() => {
        if (role === "ADMIN") {
            getThresholds(groupId).then(setThresholds);
        }
        if (role !== "RESIDENT") {
            getReactions(groupId).then(setReactions);
        }
    }, [groupId, role]);

    return (
        <div>
            <h2>Group details</h2>

            {role === "ADMIN" && <ThresholdList thresholds={thresholds} />}
            {role !== "RESIDENT" && <ReactionList reactions={reactions} />}
        </div>
    );
};

export default GroupDetailsPage;
