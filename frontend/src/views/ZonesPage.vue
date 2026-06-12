<template>
  <div>
    <h2>Zones</h2>
    <ul>
      <li v-for="z in list" :key="z.id">{{z.name}} (warehouse: {{z.warehouse?.name || 'N/A'}})</li>
    </ul>
    <form @submit.prevent="create">
      <input v-model="form.name" placeholder="name" />
      <input v-model.number="form.warehouseId" placeholder="warehouse id" />
      <button>Save</button>
    </form>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() { return { list: [], form: { name: '', warehouseId: null } } },
  mounted() { this.load() },
  methods: {
    async load() { const res = await axios.get('/api/zones'); this.list = res.data },
    async create() {
      const payload = { name: this.form.name, warehouse: { id: this.form.warehouseId } }
      await axios.post('/api/zones', payload);
      this.form.name=''; this.form.warehouseId=null; this.load()
    }
  }
}
</script>
