<template>
  <router-view />
</template>

<script setup>
</script>

<style>
:root {
  --bg: #0d1117;
  --card: #161b22;
  --border: #30363d;
  --text: #e6edf3;
  --text-muted: #8b949e;
  --accent: #238636;
  --accent-hover: #2ea043;
  --danger: #f85149;
  --warning: #d29922;
  --radius: 8px;
  --font: 'DM Sans', 'Noto Sans SC', -apple-system, sans-serif;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: var(--font);
  background: var(--bg);
  color: var(--text);
  min-height: 100vh;
  line-height: 1.5;
}

#app {
  min-height: 100vh;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.5rem 1rem;
  border-radius: var(--radius);
  font: inherit;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: background 0.2s, color 0.2s;
}

.btn-primary {
  background: var(--accent);
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  background: var(--accent-hover);
}

.btn-secondary {
  background: var(--border);
  color: var(--text);
}

.btn-secondary:hover:not(:disabled) {
  background: #484f58;
}

.btn-danger {
  background: #da3633;
  color: #fff;
}

.btn-danger:hover:not(:disabled) {
  background: #b62324;
}

.input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--bg);
  color: var(--text);
  font: inherit;
}

.input:focus {
  outline: none;
  border-color: var(--accent);
}

.label {
  display: block;
  margin-bottom: 0.25rem;
  font-size: 0.875rem;
  color: var(--text-muted);
}

.card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 1.25rem;
}

.error-msg {
  color: var(--danger);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.success-msg {
  color: var(--accent);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

table.data-table {
  width: 100%;
  border-collapse: collapse;
}

table.data-table th,
table.data-table td {
  padding: 0.75rem 1rem;
  text-align: left;
  border-bottom: 1px solid var(--border);
}

table.data-table th {
  font-weight: 600;
  color: var(--text-muted);
  font-size: 0.8125rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

table.data-table tr:hover td {
  background: rgba(35, 134, 54, 0.06);
}
</style>
