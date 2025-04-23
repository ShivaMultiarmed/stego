# Анализ изменений значений AUMP (асимптотически наиболее мощный тест)

import sys

filepath = str(sys.argv[1])
values = []

# Считываем все значения AUMP
with open(filepath, 'r') as f:
    for line in f:
        line = line.strip()
        if not line:
            continue
        idx, val_str = line.split()
        values.append(float(val_str))

# Подсчёт изменений между соседними значениями
increases = sum(1 for i in range(0, len(values) - 1, 2) if values[i] < values[i + 1])
decreases = sum(1 for i in range(0, len(values) - 1, 2) if values[i] > values[i + 1])
equals = sum(1 for i in range(1, len(values)) if values[i] == values[i-1])
total_pairs = len(values) - 1

print(f"Всего значений: {len(values)}")
print(f"Проверок соседей: {total_pairs}")
print(f"Увеличения: {increases} ({increases/total_pairs:.1%})")
print(f"Уменьшения: {decreases} ({decreases/total_pairs:.1%})")
print(f"Равные: {equals} ({equals/total_pairs:.1%})")
