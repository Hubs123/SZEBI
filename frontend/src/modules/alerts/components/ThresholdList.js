import React, { useState } from "react";

const ThresholdList = ({ thresholds }) => {
    const [localThresholds, setLocalThresholds] = useState(thresholds);

    return (
        <div>
            <h3>Thresholds</h3>

            <table>
                <thead>
                <tr>
                    <th>Type</th>
                    <th>Warning</th>
                    <th>Emergency</th>
                </tr>
                </thead>
                <tbody>
                {localThresholds.map(threshold => (
                    <tr key={threshold.id}>
                        <td>{threshold.thresholdType}</td>
                        <td>{threshold.valueWarning}</td>
                        <td>{threshold.valueEmergency}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default ThresholdList;
