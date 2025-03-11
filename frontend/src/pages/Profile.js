import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Badge } from 'react-bootstrap';
import axios from 'axios';
import AuthService from '../services/AuthService';

const Profile = ({ currentUser }) => {
  const [gameHistory, setGameHistory] = useState([]);
  const [chipTransactions, setChipTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setLoading(true);
        setError('');

        // 获取用户游戏历史
        const historyResponse = await axios.get(`/users/${currentUser.userId}/game-history`, {
          headers: AuthService.getAuthHeader()
        });

        // 获取用户筹码交易记录
        const transactionsResponse = await axios.get(`/users/${currentUser.userId}/chip-transactions`, {
          headers: AuthService.getAuthHeader()
        });

        setGameHistory(historyResponse.data || []);
        setChipTransactions(transactionsResponse.data || []);
        setLoading(false);
      } catch (error) {
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        
        setError(resMessage);
        setLoading(false);
      }
    };

    if (currentUser) {
      fetchUserData();
    }
  }, [currentUser]);

  // 计算胜率
  const calculateWinRate = () => {
    if (!currentUser || currentUser.totalGames === 0) {
      return 0;
    }
    return ((currentUser.wins / currentUser.totalGames) * 100).toFixed(2);
  };

  // 计算总盈亏
  const calculateTotalProfit = () => {
    if (!chipTransactions || chipTransactions.length === 0) {
      return 0;
    }

    return chipTransactions.reduce((total, transaction) => {
      if (transaction.transactionType === 'WIN') {
        return total + transaction.amount;
      } else if (transaction.transactionType === 'LOSE') {
        return total - transaction.amount;
      }
      return total;
    }, 0);
  };

  return (
    <Container>
      <h2 className="mb-4">个人资料</h2>

      <Row>
        <Col md={4}>
          <Card className="mb-4">
            <Card.Header>用户信息</Card.Header>
            <Card.Body>
              <p><strong>用户名:</strong> {currentUser.username}</p>
              <p><strong>当前筹码:</strong> {currentUser.currentChips}</p>
              <p><strong>注册时间:</strong> {new Date(currentUser.createdAt).toLocaleDateString()}</p>
              <p><strong>上次登录:</strong> {new Date(currentUser.lastLogin).toLocaleDateString()}</p>
            </Card.Body>
          </Card>

          <Card className="mb-4">
            <Card.Header>游戏统计</Card.Header>
            <Card.Body>
              <p><strong>总游戏局数:</strong> {currentUser.totalGames || 0}</p>
              <p><strong>胜利局数:</strong> {currentUser.wins || 0}</p>
              <p><strong>胜率:</strong> {calculateWinRate()}%</p>
              <p><strong>总盈亏:</strong> 
                <span className={calculateTotalProfit() >= 0 ? 'text-success' : 'text-danger'}>
                  {' '}{calculateTotalProfit() >= 0 ? '+' : ''}{calculateTotalProfit()}
                </span>
              </p>
            </Card.Body>
          </Card>
        </Col>

        <Col md={8}>
          <Card className="mb-4">
            <Card.Header>游戏历史</Card.Header>
            <Card.Body>
              {loading ? (
                <p className="text-center">加载中...</p>
              ) : error ? (
                <p className="text-danger">{error}</p>
              ) : gameHistory.length === 0 ? (
                <p className="text-center">暂无游戏记录</p>
              ) : (
                <Table striped bordered hover responsive>
                  <thead>
                    <tr>
                      <th>游戏ID</th>
                      <th>房间</th>
                      <th>开始时间</th>
                      <th>结束时间</th>
                      <th>初始筹码</th>
                      <th>最终筹码</th>
                      <th>结果</th>
                      <th>最终牌型</th>
                    </tr>
                  </thead>
                  <tbody>
                    {gameHistory.map(game => (
                      <tr key={game.id.gameId}>
                        <td>{game.id.gameId}</td>
                        <td>{game.game.room.name}</td>
                        <td>{new Date(game.game.startTime).toLocaleString()}</td>
                        <td>{game.game.endTime ? new Date(game.game.endTime).toLocaleString() : '进行中'}</td>
                        <td>{game.initialChips}</td>
                        <td>{game.finalChips || '-'}</td>
                        <td>
                          {game.isWinner ? (
                            <Badge bg="success">获胜</Badge>
                          ) : game.game.status === 'COMPLETED' ? (
                            <Badge bg="danger">失败</Badge>
                          ) : (
                            <Badge bg="warning">进行中</Badge>
                          )}
                        </td>
                        <td>{game.finalHandType || '-'}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>

          <Card>
            <Card.Header>筹码交易记录</Card.Header>
            <Card.Body>
              {loading ? (
                <p className="text-center">加载中...</p>
              ) : error ? (
                <p className="text-danger">{error}</p>
              ) : chipTransactions.length === 0 ? (
                <p className="text-center">暂无交易记录</p>
              ) : (
                <Table striped bordered hover responsive>
                  <thead>
                    <tr>
                      <th>交易ID</th>
                      <th>交易类型</th>
                      <th>金额</th>
                      <th>游戏ID</th>
                      <th>交易时间</th>
                    </tr>
                  </thead>
                  <tbody>
                    {chipTransactions.map(transaction => (
                      <tr key={transaction.id}>
                        <td>{transaction.id}</td>
                        <td>
                          <Badge bg={
                            transaction.transactionType === 'WIN' ? 'success' :
                            transaction.transactionType === 'LOSE' ? 'danger' :
                            transaction.transactionType === 'BUY_IN' ? 'info' :
                            transaction.transactionType === 'CASH_OUT' ? 'warning' :
                            'secondary'
                          }>
                            {transaction.transactionType === 'WIN' ? '赢得筹码' :
                             transaction.transactionType === 'LOSE' ? '输掉筹码' :
                             transaction.transactionType === 'BUY_IN' ? '买入' :
                             transaction.transactionType === 'CASH_OUT' ? '取出' :
                             transaction.transactionType}
                          </Badge>
                        </td>
                        <td className={
                          transaction.transactionType === 'WIN' || transaction.transactionType === 'CASH_OUT' 
                            ? 'text-success' 
                            : 'text-danger'
                        }>
                          {transaction.transactionType === 'WIN' || transaction.transactionType === 'CASH_OUT' 
                            ? '+' 
                            : '-'
                          }
                          {Math.abs(transaction.amount)}
                        </td>
                        <td>{transaction.game ? transaction.game.id : '-'}</td>
                        <td>{new Date(transaction.transactionTime).toLocaleString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Profile; 