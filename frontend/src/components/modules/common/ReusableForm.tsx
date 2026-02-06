// ReusableForm.jsx
import React from 'react';
import styles from './FormElement.module.scss';

const ReusableForm = ({ title, children, onSubmit, onCancel }) => {
    return (
        <form className={styles.form} onSubmit={onSubmit}>
            <h3>{title}</h3>
            {children}
            <div className={styles.formButtons}>
                <button type="button" className={styles.buttonRemove} onClick={onCancel}>
                    Cancel
                </button>
                <button type="submit" className={styles.submitButton}>
                    Submit
                </button>
            </div>
        </form>
    );
};

export default ReusableForm;
