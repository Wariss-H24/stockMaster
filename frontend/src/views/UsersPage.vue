<template>
  <div>
    <h2>Users</h2>
    <ul>
      <li v-for="u in users" :key="u.id">{{u.username}} ({{u.fullName}})</li>
    </ul>
    <form @submit.prevent="create">
      <input v-model="form.username" placeholder="username" />
      <input v-model="form.fullName" placeholder="full name" />
      <button>Save</button>
    </form>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data() { return { users: [], form: { username: '', fullName: '' } } },
  mounted() { this.load() },
  methods: {
    async load() { const res = await axios.get('/api/users'); this.users = res.data },
    async create() { await axios.post('/api/users', this.form); this.form.username=''; this.form.fullName=''; this.load() }
  }
}
</script>
