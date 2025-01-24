/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.blockchainbiz.erc20;

/** ERC20 constants for KEYS ,EVENTS and MSP */
public enum ContractConstants {
  BALANCE_PREFIX("balance"),
  BURN_BALANCE_PREFIX("burnBalance"),
  ALLOWANCE_PREFIX("allowance"),
  NAME_KEY("name"),
  NAME_VALUE("IntrinsicCoin"),
  SYMBOL_KEY("symbolKey"),
  SYMBOL_VALUE("COIN"),
  DECIMALS_KEY("decimals"),
  TOTAL_SUPPLY_KEY("totalSupply"),
  TRANSFER_EVENT("Transfer"),
  ORG1("YachtSales"),
  ORG2("FurnituresMakers"),
  ORG3("WoodSupply"),
  APPROVAL("Approval"),
  USED_TRANSACTIONS_PREFIX("USED_TRANSACTIONS_"),
  BURN_TRANSACTIONS_PREFIX("BURN_TRANSACTIONS_"),
  IBAN_KEY("currentIBAN"),
  VOTES_KEY_PREFIX("voteFor_");


  private final String prefix;

  ContractConstants(final String value) {
    this.prefix = value;
  }

  public String getValue() {
    return prefix;
  }
}
