<template>
  <div>
    <h2>Products</h2>
    <ul>
      <li v-for="p in list" :key="p.id">{{p.reference}} - {{p.name}} - {{p.category}}</li>
    </ul>
    <form @submit.prevent="create">
      <input v-model="form.reference" placeholder="reference" />
      <input v-model="form.name" placeholder="name" />
      <input v-model="form.category" placeholder="category" />
      <button>Save</button>
    </form>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() { return { list: [], form: { reference: '', name: '', category: '' } } },
  mounted() { this.load() },
  methods: {
    async load() { const res = await axios.get('/api/products'); this.list = res.data },
    async create() { await axios.post('/api/products', this.form); this.form.reference=''; this.form.name=''; this.form.category=''; this.load() }
  }
}
</script>
