import React from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Col, Card, Button } from 'react-bootstrap';

const Home = () => {
  return (
    <Container>
      <Row className="mt-5">
        <Col md={12} className="text-center">
          <h1 className="display-4">欢迎来到德州扑克游戏</h1>
          <p className="lead">
            体验刺激的德州扑克游戏，与其他玩家一起竞技，展示你的扑克技巧！
          </p>
        </Col>
      </Row>

      <Row className="mt-5">
        <Col md={4}>
          <Card className="mb-4 shadow-sm">
            <Card.Body>
              <Card.Title>创建房间</Card.Title>
              <Card.Text>
                创建自己的游戏房间，邀请朋友一起玩，设置自己的游戏规则。
              </Card.Text>
              <Link to="/rooms">
                <Button variant="primary">开始创建</Button>
              </Link>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="mb-4 shadow-sm">
            <Card.Title>加入游戏</Card.Title>
            <Card.Body>
              <Card.Text>
                浏览现有的游戏房间，加入其他玩家的游戏，立即开始比赛。
              </Card.Text>
              <Link to="/rooms">
                <Button variant="primary">查看房间</Button>
              </Link>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="mb-4 shadow-sm">
            <Card.Title>个人资料</Card.Title>
            <Card.Body>
              <Card.Text>
                查看你的游戏统计数据，包括胜率、筹码变化和游戏历史记录。
              </Card.Text>
              <Link to="/profile">
                <Button variant="primary">查看资料</Button>
              </Link>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Row className="mt-5">
        <Col md={12}>
          <Card className="mb-4 shadow-sm">
            <Card.Body>
              <Card.Title>游戏规则</Card.Title>
              <Card.Text>
                <h5>基本规则</h5>
                <p>
                  德州扑克是一种流行的扑克游戏，每位玩家有两张底牌，桌面上有五张公共牌。
                  玩家需要使用自己的底牌和公共牌组成最好的五张牌组合。
                </p>
                <h5>牌型大小（从小到大）</h5>
                <ul>
                  <li>高牌 (High Card)</li>
                  <li>一对 (One Pair)</li>
                  <li>两对 (Two Pair)</li>
                  <li>三条 (Three of a Kind)</li>
                  <li>顺子 (Straight)</li>
                  <li>同花 (Flush)</li>
                  <li>葫芦 (Full House)</li>
                  <li>四条 (Four of a Kind)</li>
                  <li>同花顺 (Straight Flush)</li>
                  <li>皇家同花顺 (Royal Flush)</li>
                </ul>
                <h5>游戏流程</h5>
                <ol>
                  <li>发两张底牌给每位玩家</li>
                  <li>第一轮下注</li>
                  <li>发三张公共牌（翻牌）</li>
                  <li>第二轮下注</li>
                  <li>发第四张公共牌（转牌）</li>
                  <li>第三轮下注</li>
                  <li>发第五张公共牌（河牌）</li>
                  <li>最后一轮下注</li>
                  <li>摊牌，牌型最大的玩家获胜</li>
                </ol>
              </Card.Text>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Home; 