// 📊 简单的状态机实现
// 学习点：状态模式

class StateMachine {
  constructor(states, initialState) {
    this.states = states;
    this.currentState = initialState;
    this.history = [initialState];
  }
  
  transition(action) {
    const currentStateConfig = this.states[this.currentState];
    
    if (!currentStateConfig || !currentStateConfig[action]) {
      console.log(`❌ 无效转换: ${this.currentState} -> ${action}`);
      return false;
    }
    
    const { next, onTransition } = currentStateConfig[action];
    
    console.log(`${this.currentState} --[${action}]--> ${next}`);
    
    if (onTransition) {
      onTransition(this.currentState, next);
    }
    
    this.currentState = next;
    this.history.push(next);
    
    return true;
  }
  
  getState() {
    return this.currentState;
  }
  
  getHistory() {
    return this.history;
  }
}

// 订单状态机示例
const orderStates = {
  created: {
    pay: {
      next: 'paid',
      onTransition: () => console.log('  ✅ 支付成功')
    },
    cancel: {
      next: 'cancelled',
      onTransition: () => console.log('  ❌ 订单已取消')
    }
  },
  paid: {
    ship: {
      next: 'shipped',
      onTransition: () => console.log('  📦 已发货')
    }
  },
  shipped: {
    receive: {
      next: 'completed',
      onTransition: () => console.log('  🎉 订单完成')
    }
  },
  completed: {},
  cancelled: {}
};

const order = new StateMachine(orderStates, 'created');

console.log('订单状态机演示:\n');
order.transition('pay');
order.transition('ship');
order.transition('receive');

console.log('\n状态历史:', order.getHistory().join(' → '));
