import React, { Component } from 'react';
import Input from "./Input"
import Messages from "./Messages";
import './App.css';

class App extends Component {
  constructor() {
    super();
    this.state = {
      value: '',
      serverResponse: '',
      messages: [
        {
          text: "Hi! Ask me a question and I'll answer!",
          fromBot: true,
        }
      ]
    }
  }

  handleSubmit(event) {
    fetch(`http://localhost:5901/search?algo=bm25&k1=0.5&b=0.45&q=${this.state.value}`)
      .then(result => result.text())
      .then(data => {
        this.setState({ serverResponse: data })
      })
    event.preventDefault()
  }

  handleChange(event) {
    this.setState({ value: event.target.value });
  }

  onSendMessage = (message) => {
    const messages = this.state.messages
    messages.push({
      text: message,
      fromBot: false
    })
    this.setState({ messages: messages })
    fetch(`http://localhost:5901/search?algo=bm25&k1=0.5&b=0.45&q=${message}`)
      .then(result => result.text())
      .then(data => {
        const updatedMessages = this.state.messages
        updatedMessages.push({
          text: data,
          fromBot: true
        })
        this.setState({ messages: updatedMessages })
      })

  }


  render() {
    return (
      <div className="App">

        <div className="App-header">
          <h1>Chatbot</h1>
        </div>
        <Messages
          messages={this.state.messages}
        />
        <Input
          onSendMessage={this.onSendMessage}
        />
      </div>
    );
  }
}

export default App;
