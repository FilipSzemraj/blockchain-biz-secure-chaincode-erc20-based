import React from 'react';
import styles from './Hamburger.module.scss';

interface HamburgerProps {
    onClick: () => void;
}

const Hamburger: React.FC<HamburgerProps> = ({onClick}) => {
    return (
        <button className={styles.hamburger} onClick={onClick} aria-label="Toggle menu">
            <span className={styles.bar}></span>
            <span className={styles.bar}></span>
            <span className={styles.bar}></span>
        </button>
    );
};

export default Hamburger;