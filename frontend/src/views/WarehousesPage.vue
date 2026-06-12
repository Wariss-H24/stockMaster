<template>
  <div>
    <h2>Warehouses</h2>
    <ul>
      <li v-for="w in list" :key="w.id">{{w.name}} - {{w.address}}</li>
    </ul>
    <form @submit.prevent="create">
      <input v-model="form.name" placeholder="name" />
      <input v-model="form.address" placeholder="address" />
      <button>Save</button>
    </form>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() { return { list: [], form: { name: '', address: '' } } },
  mounted() { this.load() },
  methods: {
    async load() { const res = await axios.get('/api/warehouses'); this.list = res.data },
    async create() { await axios.post('/api/warehouses', this.form); this.form.name=''; this.form.address=''; this.load() }
  }
}
</script>
