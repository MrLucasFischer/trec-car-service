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
          photo: false
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
      fromBot: false,
      photo: false
    })
    this.setState({ messages: messages })

    

    //Get Server response to user query
    fetch(`http://localhost:5901/search?algo=bm25&k1=0.5&b=0.45&q=${message}`)
      .then(result => result.text())
      .then(data => {

        //Add to messages, one with the server response to the query,
        //Another with the photo from flicker

        const updatedMessages = this.state.messages
        updatedMessages.push({
          text: data,
          fromBot: true,
          photo: false
        })
        this.setState({ messages: updatedMessages }, () => {
          //Get Photo from Flicker
          this.getFlickerPhoto(message)
        })

      })

  }

  getFlickerPhoto(message) {
    var queryParams = '?method=flickr.photos.search&api_key=e53b26790d51fd55a2d65d7288dc8ae6';
    queryParams += '&sort=relevance&text=' + message + '&format=json&nojsoncallback=1';
    var topImageURL = "";
    fetch("https://api.flickr.com/services/rest/" + queryParams)
      .then(result => result.json())
      .then(data => {
        console.log(data)
        if (data.stat === "ok") {
          if (data.photos.photo.length !== 0) {
            var topPhoto = data.photos.photo[0];
            topImageURL = `https://farm${topPhoto.farm}.staticflickr.com/${topPhoto.server}/${topPhoto.id}_${topPhoto.secret}.jpg`;
            const updatedMessages = this.state.messages
            updatedMessages.push({
              text: topImageURL,
              fromBot: true,
              photo: true
            })
            this.setState({messages: updatedMessages})
          }
        }
      })
  }



  resetMessages = () => {
    this.setState({
      messages: [
        {
          text: "Hi! Ask me a question and I'll answer!",
          fromBot: true,
        }
      ]
    })
  }


  render() {
    return (
      <div className="App">

        <div className="App-header">
          <h1>GoLocal</h1>
        </div>

        <Messages messages={this.state.messages} />

        <Input onSendMessage={this.onSendMessage} resetMessages={this.resetMessages} />
      </div>
    );
  }
}

export default App;
