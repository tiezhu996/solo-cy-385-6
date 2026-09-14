import { createApp } from 'vue';
import { Button, Cell, CellGroup, Collapse, CollapseItem, Empty, Search, Stepper, Tag } from 'vant';
import 'vant/lib/index.css';
import App from './App.vue';
import './styles.css';

createApp(App)
  .use(Button).use(Cell).use(CellGroup).use(Tag)
  .use(Collapse).use(CollapseItem).use(Empty).use(Search).use(Stepper)
  .mount('#app');
