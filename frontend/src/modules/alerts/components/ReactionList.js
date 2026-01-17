import React from "react";

const ReactionList = ({ reactions }) => {
    return (
        <div>
            <h3>Automatic Reactions</h3>

            <ul>
                {reactions.map(reaction => (
                    <li key={reaction.id}>
                        {reaction.name}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default ReactionList;
