# -*- coding: utf-8 -*-
"""
Created on Thu Jan  2 14:39:39 2025

@author: filip
"""

from dataclasses import dataclass
from typing import Optional
from datetime import datetime
import json
import os
from cryptography.hazmat.primitives import serialization, hashes
from cryptography.hazmat.primitives.asymmetric import rsa, padding
import hashlib
import base64




script_directory = os.path.dirname(os.path.abspath(__file__))
#print("Ścieżka do katalogu skryptu:", script_directory)

@dataclass
class Confirmation:
    refNumber: str
    amount: int
    transferData: str
    fromIBAN: str
    toIBAN: str
    rawData: str

    def to_genson_json(self) -> str:
        """
        Serializes the instance to a JSON format compatible with Genson.
        """
        data = {
            "amount": self.amount,
            "fromIBAN": self.fromIBAN,
            "rawData": self.rawData,
            "refNumber": self.refNumber,
            "toIBAN": self.toIBAN,
            "transferData": self.transferData,
        }
        return json.dumps(data, ensure_ascii=False, separators=(',', ':'))
    
    @staticmethod
    def load_private_key(file_path: str):
        """
        Loads a private key from a PEM file.
        """
        with open(file_path, "rb") as key_file:
            private_key = serialization.load_pem_private_key(
                key_file.read(),
                password=None  # Podaj hasło, jeśli klucz jest zabezpieczony
            )
        return private_key

    @staticmethod
    def sign_hash(private_key, data: bytes) -> bytes:
        """
        Signs the hash using the private key.
        """
        return private_key.sign(
            data,
            padding.PKCS1v15(),
            hashes.SHA256()
        )
    
    def save_to_file(self, file_path: str, private_key_path: str):
        """
        Saves the serialized JSON to a file with a computed and signed SHA-256 hash.
        """
        # Serialize object
        serialized_data = self.to_genson_json()

        # Compute SHA-256 hash
        sha256_hash = hashlib.sha256(serialized_data.encode('utf-8')).hexdigest()

        # Load the private key
        private_key = self.load_private_key(private_key_path)

        # Sign the hash
        signed_hash = self.sign_hash(private_key, sha256_hash.encode('utf-8'))

        # Encode the signed hash in Base64 for easier storage in JSON
        signed_hash_base64 = base64.b64encode(signed_hash).decode('utf-8')

        # Create JSON with signed hash and data
        json_with_signed_hash = {
            "hash": signed_hash_base64,
            "data": json.loads(serialized_data)
        }

        # Save to file
        with open(file_path, 'w', encoding='utf-8') as file:
            json.dump(json_with_signed_hash, file, ensure_ascii=False, separators=(',', ':'))
        print(f"Serialized JSON with signed hash saved to {file_path}")

# Przykład użycia
if __name__ == "__main__":
    confirmation = Confirmation(
        refNumber="123ABC",
        amount=1000,
        transferData=datetime(2025, 1, 2, 12, 0, 0, 0).isoformat(),
        fromIBAN="PL0000000000000000000000000",
        toIBAN="PL9999999999999999999999999",
        rawData="EUR"
    )
    
    genson_json = confirmation.to_genson_json()
    print(f"Genson-compatible JSON: {genson_json}")

    # Oblicz skrót SHA-256
    sha256_hash = hashlib.sha256(genson_json.encode("utf-8")).hexdigest()
    print(f"SHA-256 Hash: {sha256_hash}")
    
    private_key_path = os.path.join(script_directory, "private_key.pem")

    file_path = os.path.join(script_directory, "confirmation.json")

    confirmation.save_to_file(file_path, private_key_path)
