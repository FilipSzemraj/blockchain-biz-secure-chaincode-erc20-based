import React, {useEffect, useRef, useState} from "react";
import styles from "./ReusableWindow.module.scss";

import { LuRefreshCw } from "react-icons/lu";

interface ButtonProps {
    text: string;
    onClick: () => void;
    className?: string;
}
interface ReusableWindowInterface {
    isOpen: boolean;
    onToggle: (newState: boolean) => void;
    buttonText: string;
    refreshFunction: () => void;
    children?: React.ReactNode;
    classNameWrapper?: string;
    classNameContent?: string;
    buttons?: ButtonProps[];
}

const ReusableWindow: React.FC<ReusableWindowInterface> = React.memo(({
                                                               isOpen,
                                                               onToggle,
                                                               buttonText,
                                                               refreshFunction,
                                                               children,
                                                               className,
                                                               classNameWrapper,
                                                               classNameContent,
                                                               buttons = []}) => {
    const handleToggle = () => onToggle(!isOpen);
    const contentRef = useRef<HTMLDivElement | null>(null);
    const [contentHeight, setContentHeight] = useState(0);

    useEffect(() => {
        if (contentRef.current) {
            if ("scrollHeight" in contentRef.current) {
                setContentHeight(isOpen ? contentRef.current.scrollHeight : 0);
            }
        }
    }, [isOpen]);

    return(
        <div className={`${styles.wrapper} ${className || ""} ${classNameWrapper || ""}`}>
            <div className={styles.wrapper__buttons}>

                <div className={styles.inner__buttons}>
                    <button
                        className={`${styles.button} ${styles.button__refresh} ${isOpen ? styles.button__inner__open : styles.button__inner__closed}`}
                        onClick={() => refreshFunction()}>
                        <LuRefreshCw/>
                    </button>
                    {buttons.map((btn, index) => (
                        <button
                            key={index}
                            className={`${styles.button} ${btn.className || ""} ${isOpen ? styles.button__inner__open : styles.button__inner__closed}`}
                            onClick={btn.onClick}
                        >
                            {btn.text}
                        </button>
                    ))}
                </div>

                <button className={`${styles.button} ${styles.button__open}`}
                        onClick={() => handleToggle()}>
                    {buttonText}
                </button>

            </div>
            {/*isOpen &&*/}
            {(
                <div ref={contentRef}
                     className={`${styles.wrapper__content} ${isOpen ? '' : styles.wrapper__content__closed}`}
                     style={{maxHeight: `${contentHeight}px`}}
                >


                    <div className={`${styles.content} ${classNameContent || ""}`}>
                        {children}
                    </div>
                </div>
            )}
        </div>
    );
});

export default ReusableWindow;