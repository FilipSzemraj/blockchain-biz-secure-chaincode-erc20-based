# -*- coding: utf-8 -*-
"""
Created on Wed Jan  1 22:06:55 2025

@author: filip
"""
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives import serialization, hashes
import os

# Ustalenie ścieżki katalogu, w którym znajduje się skrypt
script_directory = os.path.dirname(os.path.abspath(__file__))
print("Ścieżka do katalogu skryptu:\n", script_directory)

private_key_path = os.path.join(script_directory, "private_key.pem")
public_key_path = os.path.join(script_directory, "public_key.pem")

# Wczytanie klucza prywatnego
try:
    with open(private_key_path, "rb") as file:
        private_key = serialization.load_pem_private_key(
            file.read(),
            password=None  # Podaj hasło, jeśli klucz jest zaszyfrowany
        )
except FileNotFoundError:
    print(f"\nPlik {private_key_path} nie został znaleziony.")
    exit(1)

# Wczytanie klucza publicznego
try:
    with open(public_key_path, "rb") as file:
        public_key = serialization.load_pem_public_key(file.read())
except FileNotFoundError:
    print(f"Plik {public_key_path} nie został znaleziony.")
    exit(1)

# Wiadomość do zaszyfrowania
message = "To jest testowa wiadomość!".encode('utf-8')
#message = b'Blockchain'

# Szyfrowanie kluczem publicznym
ciphertext = public_key.encrypt(
    message,
    padding.OAEP(
        mgf=padding.MGF1(algorithm=hashes.SHA256()),
        algorithm=hashes.SHA256(),
        label=None
    )
)

print(f"Zaszyfrowana wiadomość: {ciphertext}\n")

# Odszyfrowanie kluczem prywatnym
plaintext = private_key.decrypt(
    ciphertext,
    padding.OAEP(
        mgf=padding.MGF1(algorithm=hashes.SHA256()),
        algorithm=hashes.SHA256(),
        label=None
    )
)

print(f"Odszyfrowana wiadomość: \n{plaintext.decode('utf-8')}")
