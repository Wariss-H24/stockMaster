<template>
  <div>
    <h1>StockMaster</h1>
    <nav>
      <router-link to="/users">Users</router-link> |
      <router-link to="/warehouses">Warehouses</router-link> |
      <router-link to="/zones">Zones</router-link> |
      <router-link to="/products">Products</router-link>
    </nav>
    <router-view />
  </div>
</template>

<script>
export default { name: 'App' }
</script>

<style>
body { font-family: Arial, sans-serif; padding: 1rem }
nav a { margin-right: .5rem }
</style>
