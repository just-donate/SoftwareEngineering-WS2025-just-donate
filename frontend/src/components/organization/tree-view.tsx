import React, { useState, useRef, useEffect, useCallback, useMemo } from 'react'
import { useTheme } from '@/contexts/ThemeContext'
import { BankAccount } from '../../types/types'

interface TreeNodeProps {
  account: BankAccount
  x: number
  y: number
  width: number
  height: number
  onPositionChange: (id: string, x: number, y: number, width: number, height: number) => void
}

const TreeNode: React.FC<TreeNodeProps> = React.memo(({ account, x, y, width, height, onPositionChange }) => {
  const theme = useTheme()
  const nodeRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (nodeRef.current) {
      const { width: actualWidth, height: actualHeight } = nodeRef.current.getBoundingClientRect()
      onPositionChange(account.id, x, y, actualWidth, actualHeight)
    }
  }, [account.id, x, y, onPositionChange])

  return (
    <div 
      ref={nodeRef}
      className="absolute rounded-md p-2"
      style={{ 
        left: x, 
        top: y, 
        width, 
        height,
        backgroundColor: theme.theme.card,
        color: theme.theme.text
      }}
    >
      <div className="font-semibold">{account.name}</div>
      <div className="text-sm" style={{ color: theme.theme.text }}>${account.balance}</div>
    </div>
  )
})

TreeNode.displayName = 'TreeNode'

interface TreeViewProps {
  accounts: BankAccount[]
}

export const TreeView: React.FC<TreeViewProps> = ({ accounts }) => {
  const [nodePositions, setNodePositions] = useState<Record<string, { x: number, y: number, width: number, height: number }>>({})
  const svgRef = useRef<SVGSVGElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)
  const [dimensions, setDimensions] = useState({ width: 1000, height: 600 })
  const [maxLevel, setMaxLevel] = useState(0)

  const handlePositionChange = useCallback((id: string, x: number, y: number, width: number, height: number) => {
    setNodePositions(prev => {
      if (prev[id]?.x === x && prev[id]?.y === y && prev[id]?.width === width && prev[id]?.height === height) {
        return prev;
      }
      return { ...prev, [id]: { x, y, width, height } };
    })
  }, [])

  const drawConnections = useCallback(() => {
    return accounts.flatMap(account =>
      account.parentIds.map(parentId => {
        const start = nodePositions[parentId]
        const end = nodePositions[account.id]
        if (start && end) {
          const startX = start.x + start.width
          const startY = start.y + start.height / 2
          const endX = end.x
          const endY = end.y + end.height / 2
          const midX = (startX + endX) / 2

          return (
            <path
              key={`${parentId}-${account.id}`}
              d={`M${startX},${startY} C${midX},${startY} ${midX},${endY} ${endX},${endY}`}
              fill="none"
              stroke="black"
              strokeWidth="1"
            />
          )
        }
        return null
      })
    ).filter(Boolean)
  }, [accounts, nodePositions])

  const hierarchy = useMemo(() => {
    // const accountMap = new Map(accounts.map(account => [account.id, account]))
    const rootAccounts = accounts.filter(account => account.parentIds.length === 0)
    
    const buildHierarchy = (currentAccounts: BankAccount[], level: number = 0, processedIds: Set<string> = new Set()): BankAccount[][] => {
      if (currentAccounts.length === 0) return []

      const currentLevel = currentAccounts.filter(account => !processedIds.has(account.id))
      currentLevel.forEach(account => processedIds.add(account.id))

      const nextLevelAccounts = Array.from(new Set(currentLevel.flatMap(account => 
        accounts.filter(a => a.parentIds.includes(account.id) && !processedIds.has(a.id))
      )))

      return [currentLevel, ...buildHierarchy(nextLevelAccounts, level + 1, processedIds)]
    }

    return buildHierarchy(rootAccounts)
  }, [accounts])

  useEffect(() => {
    setMaxLevel(hierarchy.length - 1)
  }, [hierarchy])

  const renderTree = useCallback(() => {
    const columnWidth = 220
    const nodeHeight = 60
    const verticalSpacing = 20

    return hierarchy.flatMap((levelAccounts, levelIndex) => 
      levelAccounts.map((account, accountIndex) => (
        <TreeNode
          key={`${levelIndex}-${account.id}`}
          account={account}
          x={levelIndex * columnWidth}
          y={accountIndex * (nodeHeight + verticalSpacing)}
          width={200}
          height={nodeHeight}
          onPositionChange={handlePositionChange}
        />
      ))
    )
  }, [hierarchy, handlePositionChange])

  useEffect(() => {
    const updateDimensions = () => {
      if (containerRef.current) {
        setDimensions({
          width: containerRef.current.offsetWidth,
          height: containerRef.current.offsetHeight
        })
      }
    }

    updateDimensions()
    window.addEventListener('resize', updateDimensions)
    return () => window.removeEventListener('resize', updateDimensions)
  }, [])

  return (
    <div ref={containerRef} className="relative w-full h-[600px] overflow-auto border rounded-md">
      <svg 
        ref={svgRef} 
        className="absolute top-0 left-0" 
        style={{ 
          width: `${Math.max((maxLevel + 1) * 220, dimensions.width)}px`, 
          height: `${dimensions.height}px` 
        }}
      >
        {drawConnections()}
      </svg>
      {renderTree()}
    </div>
  )
}

