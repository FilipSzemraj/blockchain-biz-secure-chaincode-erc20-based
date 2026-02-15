

## 📝 Title Display Rules

1. **Inside `Dokumenty`**

    * **Exactly one folder under `Dokumenty`** → show **that folder name** only.
      Example:

      ```
      /home/filip/Dokumenty/project
      → project
      ```

    * **Deeper than one folder under `Dokumenty`** → remove the first folder after `Dokumenty` and show the rest of the path.
      Example:

      ```
      /home/filip/Dokumenty/project/infrastructure/ca
      → infrastructure/ca
      ```

2. **Outside `Dokumenty`**

    * Show the **last 2 directories** of the path.
      Example:

      ```
      /var/log/nginx/conf.d
      → nginx/conf.d
      ```

3. **General Layout**

    * Prefix with `pane_index` and `pane_title`.
    * Suffix with the current command (`pane_current_command`).
    * Full format looks like:

      ```
      <pane_index> <pane_title> <path according to above rules> <pane_current_command>
      ```

---

This ensures **three clear scenarios**, avoids partial truncation like `0-based`, and handles both deep and shallow paths consistently.
