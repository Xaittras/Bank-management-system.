import { useCallback, useState } from "react";

// Локальне сховище на користувача (поки бекенд не має відповідних таблиць)
const keyOf = (userId, key) => `bankdash:${userId}:${key}`;

export function useStore(userId, key, initial) {
  const [value, setValue] = useState(() => {
    try {
      const raw = localStorage.getItem(keyOf(userId, key));
      return raw ? JSON.parse(raw) : initial;
    } catch {
      return initial;
    }
  });

  const update = useCallback(
    (next) => {
      setValue((prev) => {
        const v = typeof next === "function" ? next(prev) : next;
        try {
          localStorage.setItem(keyOf(userId, key), JSON.stringify(v));
        } catch {
          /* сховище недоступне: дані лишаються лише в пам'яті */
        }
        return v;
      });
    },
    [userId, key]
  );

  return [value, update];
}
