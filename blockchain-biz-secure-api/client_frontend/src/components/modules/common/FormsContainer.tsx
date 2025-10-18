import React, { useState } from "react";
import styles from "./FormsContainer.module.scss";

interface FormConfig {
    key: string;
    label: string;
    component: React.FC<{ setIsContainerOpen: (open: string) => void }>;
}

interface FormsContainerProps {
    isContainerOpen: string
    setIsContainerOpen: (open: string) => void;
    forms: FormConfig[];
}

const FormsContainer: React.FC<FormsContainerProps> = ({isContainerOpen, setIsContainerOpen, forms }) => {

    return (
        <div className={styles.formsWrapper}>
            <div className={`${styles.formContentWrapper} ${isContainerOpen ? styles.formContentWrapper__open : ''}`}>
                {forms.map((form) => {
                    const FormComponent = form.component;
                    return (
                        <div
                            key={form.key}
                            className={`${styles.formItem} ${form.key === isContainerOpen ? styles.active : ""}`}
                        >
                            <FormComponent setIsContainerOpen={(open: string) => setIsContainerOpen(open)} />
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default FormsContainer;
