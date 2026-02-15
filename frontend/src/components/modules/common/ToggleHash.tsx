import React, { useState } from "react";

const ToggleHash = ({ hash, truncateLength = 20 }) => {
    const [showFull, setShowFull] = useState(false);

    return (
        <span
            onClick={() => setShowFull(!showFull)}
            style={{ cursor: "pointer", color: "#1a88ff", textDecoration: "underline" }}
        >
      {showFull ? hash : `${hash.substring(0, truncateLength)}...`}
    </span>
    );
};

export default ToggleHash;
