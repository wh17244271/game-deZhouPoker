import React, { useState } from 'react';
import { Modal, Button, Form, ProgressBar } from 'react-bootstrap';
import WebSocketService from '../services/WebSocketService';

const AllinVote = ({ 
  show, 
  onHide, 
  gameId, 
  voteResults = {}, 
  mostVotedOption = null,
  hasVoted = false
}) => {
  const [selectedOption, setSelectedOption] = useState(1);
  
  // 处理投票
  const handleVote = () => {
    if (!gameId || hasVoted) return;
    
    WebSocketService.sendAllinVote(gameId, selectedOption);
  };
  
  // 计算投票百分比
  const calculatePercentage = (option) => {
    if (!voteResults || Object.keys(voteResults).length === 0) return 0;
    
    const totalVotes = Object.values(voteResults).reduce((sum, count) => sum + count, 0);
    if (totalVotes === 0) return 0;
    
    return Math.round((voteResults[option] || 0) / totalVotes * 100);
  };
  
  // 获取进度条变体
  const getProgressVariant = (option) => {
    if (mostVotedOption === option) {
      return 'success';
    }
    return 'info';
  };
  
  return (
    <Modal show={show} onHide={onHide} centered backdrop="static">
      <Modal.Header closeButton>
        <Modal.Title>All-in 投票</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p>有玩家全下了！请投票决定发几次公共牌：</p>
        
        {hasVoted || Object.keys(voteResults).length > 0 ? (
          <div className="vote-results">
            <h5>投票结果：</h5>
            {[1, 2, 3].map(option => (
              <div key={option} className="mb-3">
                <div className="d-flex justify-content-between mb-1">
                  <span>发 {option} 次牌</span>
                  <span>{voteResults[option] || 0} 票 ({calculatePercentage(option)}%)</span>
                </div>
                <ProgressBar 
                  variant={getProgressVariant(option)}
                  now={calculatePercentage(option)} 
                  label={`${calculatePercentage(option)}%`}
                />
              </div>
            ))}
            
            {mostVotedOption && (
              <div className="alert alert-success mt-3">
                <strong>当前最多票选项：</strong> 发 {mostVotedOption} 次牌
              </div>
            )}
          </div>
        ) : (
          <Form>
            <Form.Group className="mb-3">
              <Form.Label>选择发牌次数：</Form.Label>
              <div>
                {[1, 2, 3].map(option => (
                  <Form.Check
                    key={option}
                    type="radio"
                    id={`vote-option-${option}`}
                    label={`发 ${option} 次牌`}
                    name="voteOption"
                    value={option}
                    checked={selectedOption === option}
                    onChange={() => setSelectedOption(option)}
                    className="mb-2"
                  />
                ))}
              </div>
            </Form.Group>
          </Form>
        )}
        
        <div className="vote-info mt-3">
          <h6>说明：</h6>
          <ul>
            <li>发1次牌：直接发全部5张公共牌</li>
            <li>发2次牌：先发3张翻牌，然后一次发转牌和河牌</li>
            <li>发3次牌：正常发牌流程（翻牌、转牌、河牌）</li>
          </ul>
        </div>
      </Modal.Body>
      <Modal.Footer>
        {!hasVoted && (
          <Button variant="primary" onClick={handleVote}>
            投票
          </Button>
        )}
        <Button variant="secondary" onClick={onHide}>
          {hasVoted ? '关闭' : '跳过'}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default AllinVote; 