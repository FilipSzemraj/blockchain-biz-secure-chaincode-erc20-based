import styles from "./FormElement.module.scss";

const FormField = ({ label, children }) => (
    <div className={styles.formGroup}>
        <label>{label}</label>
        {children}
    </div>
);
export default FormField;
