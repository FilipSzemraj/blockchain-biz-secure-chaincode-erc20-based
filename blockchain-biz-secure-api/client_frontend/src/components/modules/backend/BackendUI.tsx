import React, {useEffect} from 'react';
import styles from './BackendUI.module.scss';
import {formatKey} from "../../../utils/text.ts";
import {AppDispatch, RootState} from "../../../redux/store.ts";
import {useDispatch, useSelector} from "react-redux";
import {fetchTokenDetailsThunk} from "../../../redux/userSlice.ts";

const BackendUI: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();

    const { tokenDetails, certificateDetails, loading, error } = useSelector(
        (state: RootState) => state.user
    );




    useEffect(() => {
        dispatch(fetchTokenDetailsThunk());
    }, [dispatch]);

    /*if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error}</p>;
    if (!tokenDetails) return <p>No token details available.</p>;*/

    return(
      <div className={styles.container}>
          {/*<h2>Token Details</h2>*/}
          {tokenDetails ? (
              <div className={styles.container__wrapper}>
                  <div className={styles.container__wrapper__firstRow}>
                      {Object.entries(tokenDetails)
                      .filter(([key]:[string, string | string[]]) => key !== "orgMspIds")
                      .map(([key, value]) => (
                      <div key={key}>
                          {formatKey(key)}: <strong>
                          {Array.isArray(value) ? value.join(", ") : value}
                      </strong>
                      </div>          ))}
                  </div>
                  <div className={styles.container__wrapper__secondRow}>
                      {Array.isArray(tokenDetails["orgMspIds"]) &&
                      certificateDetails &&
                      certificateDetails.issuer &&
                      certificateDetails.issuer.O ? (
                          <>
                              Other organizations:{" "}
                              <strong>
                                  {tokenDetails["orgMspIds"]
                                      .filter((org) => org !== certificateDetails.issuer.O)
                                      .join(", ")}
                              </strong>
                          </>
                      ) : (
                          "No other organizations."
                      )}
                  </div>
              </div>
          ) : (
              <p>Loading...</p>
          )}
      </div>
    );
}

export default BackendUI;