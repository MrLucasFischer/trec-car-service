import { Component } from "react";
import React from "react";

class Messages extends Component {

    renderMessage(message) {
        const { text, fromBot, photo } = message;
        const className = fromBot ?
            "Messages-message" : "Messages-message currentMember";
        const messageToDisplay = photo ?
        <img src={text}/> : <div className="text">{text}</div>
        return (
            <li className={className}>
                <div className="Message-content">
                    <div className="username">
                        {fromBot ? "Bot" : "User"}
                    </div>
                    {messageToDisplay}
                </div>
            </li>
        );
    }


    render() {
        const { messages } = this.props;
        return (
            <ul className="Messages-list">
                {messages.map(m => this.renderMessage(m))}
            </ul>
        );
    }
}

export default Messages;