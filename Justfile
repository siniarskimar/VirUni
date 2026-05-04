mod frontend
mod backend

default:

tmux-workspace:
    tmux new-session -d -s viruni-dev
    tmux rename-window -t viruni-dev:0 'frontend-serve'
    tmux send-keys -t viruni-dev:0 'just frontend::watch' C-m

    tmux new-window -t viruni-dev:1 -n 'backend-bootRun'

    LOG_FILE=$(mktemp)
    tmux send-keys -t viruni-dev:1 "docker compose up postgres --wait && just backend::watch >$LOG_FILE 2>&1 & tail -f $LOG_FILE" C-m

    tmux attach-session -t viruni-dev
