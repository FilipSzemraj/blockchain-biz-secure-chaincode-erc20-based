# -*- coding: utf-8 -*-
"""
Created on Wed Jan  1 21:25:45 2025

@author: filip
"""
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import serialization

import os

# Ustalenie ścieżki katalogu, w którym znajduje się skrypt
script_directory = os.path.dirname(os.path.abspath(__file__))
print("Ścieżka do katalogu skryptu:", script_directory)

# Generowanie pary kluczy
private_key = rsa.generate_private_key(
    public_exponent=65537,  # Standardowy publiczny wykładnik
    key_size=2048,          # Długość klucza w bitach (2048 lub 4096 dla RSA)
)

# Eksportowanie klucza prywatnego do PEM
private_pem = private_key.private_bytes(
    encoding=serialization.Encoding.PEM,
    format=serialization.PrivateFormat.PKCS8,
    encryption_algorithm=serialization.NoEncryption()  # Brak hasła
)

# Eksportowanie klucza publicznego do PEM
public_key = private_key.public_key()
public_pem = public_key.public_bytes(
    encoding=serialization.Encoding.PEM,
    format=serialization.PublicFormat.SubjectPublicKeyInfo
)

# Zapis do plików w katalogu skryptu
private_key_path = os.path.join(script_directory, "private_key.pem")
public_key_path = os.path.join(script_directory, "public_key.pem")

with open(private_key_path, "wb") as private_file:
    private_file.write(private_pem)

with open(public_key_path, "wb") as public_file:
    public_file.write(public_pem)

print(f"Klucze zostały zapisane w katalogu skryptu: {script_directory}")
