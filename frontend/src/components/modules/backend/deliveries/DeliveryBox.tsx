import React, { useEffect, useMemo, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import styles from './DeliveryBox.module.scss';
import ReusableWindow from '../../common/ReusableWindow.tsx';
import { Delivery } from '../../../../model/DeliveriesDetails';
import CreateDeliveryForm from './CreateDeliveryForm.tsx';
import FormsContainer from '../../common/FormsContainer.tsx';
import AddPartialDeliveryForm from './AddPartialDeliveryForm.tsx';
import ConfirmDeliveryForm from './ConfirmDeliveryForm.tsx';
import ConfirmPartialDeliveryForm from './ConfirmPartialDeliveryForm.tsx';
import ResolvePartialDeliveryDisputeForm from './ResolvePartialDeliveryDisputeForm.tsx';
import {AppDispatch, RootState} from "../../../../redux/store.ts";
import {fetchDeliveries} from "../../../../redux/deliveriesSlice.ts";
import StartDeliveryForm from "./StartDeliveryForm.tsx";

const DeliveryBox: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();

    const [sortOrder, setSortOrder] = useState<'asc' | 'desc' | null>(null);


    const { deliveries, loading, error, fetched } = useSelector(
        (state: RootState) => state.deliveries
    );

    const { certificateDetails } = useSelector(
        (state: RootState) => state.user
    );

    const [show, setShow] = useState(false);
    const [activeForm, setActiveForm] = useState<
        | 'createDelivery'
        | 'addPartialDelivery'
        | 'confirmDelivery'
        | 'confirmPartialDelivery'
        | 'resolvePartialDeliveryDispute'
        | 'startDelivery'
        | ''
    >('');
    const [selectedRole, setSelectedRole] = useState('');

    const userOrg = useMemo(() => {
        if (!certificateDetails || !certificateDetails.issuer) return '';
        return certificateDetails.issuer['O'] ? certificateDetails.issuer['O'] : '';
    }, [certificateDetails]);

    useEffect(() => {
        if (show && !fetched) {
            dispatch(fetchDeliveries());
        }
    }, [show, fetched, dispatch]);

    const handleCheckboxChange = (role: string) => {
        setSelectedRole((prevRole) => (prevRole === role ? '' : role));
    };



    const sortByDate = (order: 'asc' | 'desc') => {
        setSortOrder(order);
    };

    const filterDeliveriesByRole = (
        role: string,
        deliveries: Delivery[],
        userOrg: string
    ): Delivery[] => {
        if (!role) return deliveries;
        switch (role) {
            case 'buyer':
                return deliveries.filter((delivery) => delivery.buyerId === userOrg);
            case 'seller':
                return deliveries.filter((delivery) => delivery.sellerId === userOrg);
            case 'arbitrator':
                return deliveries.filter((delivery) => delivery.arbitratorId === userOrg);
            default:
                return deliveries;
        }
    };

    const filteredDeliveries = useMemo(() => {
        return filterDeliveriesByRole(selectedRole, deliveries, userOrg || '');
    }, [selectedRole, deliveries, userOrg]);

    const confirmableDeliveryIds = useMemo(() => {
        return filterDeliveriesByRole('buyer', deliveries, userOrg || '').map(
                (delivery) => delivery.deliveryId
            )
        /*return [
        ...filterDeliveriesByRole('buyer', deliveries, userOrg || '').map(
            (delivery) => delivery.deliveryId
        ),
        ...filterDeliveriesByRole('seller', deliveries, userOrg || '').map(
            (delivery) => delivery.deliveryId
        ),
        ]*/
    }, [deliveries, userOrg]);

    const confirmableDelivery = useMemo(() => {
        return filterDeliveriesByRole('buyer', deliveries, userOrg || '');
    }, [deliveries, userOrg]);

    const arbitratorDeliveries = useMemo(() => {
        return filterDeliveriesByRole('arbitrator', deliveries, userOrg || '');
    }, [deliveries, userOrg]);

    const sellerDeliveries = useMemo(() => {
        return filterDeliveriesByRole('seller', deliveries, userOrg || '');
    }, [deliveries, userOrg]);

    const sellerDeliveriesIds = useMemo(() => {
        return filterDeliveriesByRole('seller', deliveries, userOrg || '').map(
            (delivery) => delivery.deliveryId
        );
    }, [deliveries, userOrg]);

    const sortedDeliveries = useMemo(() => {
        if (!filteredDeliveries || !sortOrder) return filteredDeliveries;

        filteredDeliveries.map((f) => {
            console.log(f.partialDeliveries);
        })

        return [...filteredDeliveries].sort((a, b) => {
            const dateA = new Date(a.creationTimestamp).getTime();
            const dateB = new Date(b.creationTimestamp).getTime();

            return sortOrder === 'asc' ? dateA - dateB : dateB - dateA;
        });
    }, [filteredDeliveries, sortOrder]);

    const forms = [
        {
            key: 'createDelivery',
            label: 'Create Delivery',
            component: (props) => <CreateDeliveryForm {...props} userOrg={userOrg} />,
        },
        {
            key: 'addPartialDelivery',
            label: 'Add Partial Delivery',
            component: (props) => (
                <AddPartialDeliveryForm
                    {...props}
                    deliveries={sellerDeliveries}
                />
            ),
        },
        {
            key: 'confirmDelivery',
            label: 'Confirm Delivery',
            component: (props) => (
                <ConfirmDeliveryForm {...props} deliveryIds={confirmableDeliveryIds} />
            ),
        },
        {
            key: 'startDelivery',
            label: 'Start Delivery',
            component: (props) => (
                <StartDeliveryForm {...props} deliveryIds={sellerDeliveriesIds} />
            ),
        },
        {
            key: 'confirmPartialDelivery',
            label: 'Confirm Partial Delivery',
            component: (props) => (
                <ConfirmPartialDeliveryForm {...props} deliveries={confirmableDelivery} />
            ),
        },
        {
            key: 'resolvePartialDeliveryDispute',
            label: 'Resolve Partial Delivery Dispute',
            component: (props) => (
                <ResolvePartialDeliveryDisputeForm
                    {...props}
                    deliveries={arbitratorDeliveries}
                />
            ),
        },
    ];

    const setShowWrapper = (value: boolean) => {
        setShow(value);
        if (!value) {
            setActiveForm('');
        }
    };

    return (
        <div className={styles.wrapper}>
            <div
                className={`${styles.wrapper__addContainer} ${
                    activeForm ? styles.wrapper__addContainer__open : ''
                }`}
            >
                <FormsContainer
                    isContainerOpen={activeForm}
                    setIsContainerOpen={(value) =>
                        setActiveForm(value as
                            | 'createDelivery'
                            | 'addPartialDelivery'
                            | 'confirmDelivery'
                            | 'confirmPartialDelivery'
                            | 'resolvePartialDeliveryDispute'
                            | 'startDelivery'
                            | '')
                    }
                    forms={forms}
                />
            </div>
            <ReusableWindow
                isOpen={show}
                onToggle={setShowWrapper}
                buttonText="Delivery"
                refreshFunction={() => dispatch(fetchDeliveries())}
                classNameWrapper={styles.deliveryWrapper}
                classNameContent={styles.deliveryContent}
                buttons={[
                    {
                        text: 'Create Delivery',
                        onClick: () =>
                            setActiveForm(
                                activeForm === 'createDelivery' ? '' : 'createDelivery'
                            ),
                        className: styles.test,
                    },
                    {
                        text: 'Add Partial Delivery',
                        onClick: () =>
                            setActiveForm(
                                activeForm === 'addPartialDelivery' ? '' : 'addPartialDelivery'
                            ),
                        className: styles.test,
                    },
                    {
                        text: 'Confirm Delivery',
                        onClick: () =>
                            setActiveForm(
                                activeForm === 'confirmDelivery' ? '' : 'confirmDelivery'
                            ),
                        className: styles.test,
                    },
                    {
                        text: 'Confirm Partial Delivery',
                        onClick: () =>
                            setActiveForm(
                                activeForm === 'confirmPartialDelivery' ? '' : 'confirmPartialDelivery'
                            ),
                        className: styles.test,
                    },
                    {
                        text: 'Start Delivery',
                        onClick: () =>
                            setActiveForm(
                                activeForm === 'startDelivery' ? '' : 'startDelivery'
                            ),
                        className: styles.test,
                    },
                    {
                        text: 'Resolve Partial Delivery',
                        onClick: () =>
                            setActiveForm(
                                activeForm === 'resolvePartialDeliveryDispute'
                                    ? ''
                                    : 'resolvePartialDeliveryDispute'
                            ),
                        className: styles.test,
                    },
                ]}
            >
                <div className={styles.container}>
                    <div className={styles.container__header}>
                        <div className={styles.container__header__actions}>
                            <button className={styles.button}>Filter</button>
                            <button className={styles.buttonOrder} onClick={() => sortByDate('asc')}>
                                ▲
                            </button>
                            <button className={styles.buttonOrder} onClick={() => sortByDate('desc')}>
                                ▼
                            </button>
                        </div>
                        <div className={styles.container__header__filters}>
                            <label>
                                <input
                                    type="checkbox"
                                    checked={selectedRole === 'buyer'}
                                    onChange={() => handleCheckboxChange('buyer')}
                                />
                                As Buyer
                            </label>
                            <label>
                                <input
                                    type="checkbox"
                                    checked={selectedRole === 'seller'}
                                    onChange={() => handleCheckboxChange('seller')}
                                />
                                As Seller
                            </label>
                            <label>
                                <input
                                    type="checkbox"
                                    checked={selectedRole === 'arbitrator'}
                                    onChange={() => handleCheckboxChange('arbitrator')}
                                />
                                As Arbitrator
                            </label>
                        </div>
                    </div>

                    {loading && <p>Loading transactions...</p>}
                    {error && <p className={styles.error}>Error: {error}</p>}
                    {!loading && !error && filteredDeliveries.length === 0 && (
                        <p>No deliveries found.</p>
                    )}
                    {!loading && !error && filteredDeliveries.length > 0 && (
                        <div className={styles.list}>
                            {sortedDeliveries.map((delivery) => (
                                <div key={delivery.deliveryId} className={styles.list__transactionItem}>
                                    <p>
                                        <strong>Delivery ID:</strong> {delivery.deliveryId}
                                    </p>
                                    <p>
                                        <strong>Buyer:</strong> {delivery.buyerId}
                                    </p>
                                    <p>
                                        <strong>Seller:</strong> {delivery.sellerId}
                                    </p>
                                    <p>
                                        <strong>Arbitrator:</strong> {delivery.arbitratorId}
                                    </p>
                                    <p>
                                        <strong>Token Amount:</strong> {delivery.tokenAmount}
                                    </p>
                                    <p>
                                        <strong>Goods Type:</strong> {delivery.goodsType}
                                    </p>
                                    <p>
                                        <strong>Goods Quantity:</strong> {delivery.goodsQuantity}
                                    </p>
                                    <p>
                                        <strong>Status:</strong> {delivery.currentStatus}
                                    </p>
                                    <p>
                                        <strong>Creation Timestamp:</strong> {delivery.creationTimestamp}
                                    </p>

                                    {delivery.partialDeliveries &&
                                        delivery.partialDeliveries.length > 0 && (
                                            <div className={styles.partialDeliveries}>
                                                <h4>Partial Deliveries:</h4>
                                                {delivery.partialDeliveries.map((pd) => (
                                                    <div
                                                        key={pd.deliveryId}
                                                        className={styles.partialDeliveryCard}
                                                    >
                                                        <p>
                                                            <strong>Partial Delivery ID:</strong> {pd.deliveryId}
                                                        </p>
                                                        <p>
                                                            <strong>Quantity:</strong> {pd.goodsQuantity}
                                                        </p>
                                                        <p>
                                                            <strong>Status:</strong> {pd.currentStatus}
                                                        </p>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </ReusableWindow>
        </div>
    );
};

export default DeliveryBox;
